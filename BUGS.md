# Defect report — ZapasDep store

Defects found while writing this suite against
[matii1942/E-commerce](https://github.com/matii1942/E-commerce). Each one has a scenario
tagged with its id that describes the **expected** behaviour, so it fails today and starts
passing once the store is fixed:

```bash
mvn test -Dcucumber.filter.tags="@known-bug"     # all of them
mvn test -Dcucumber.filter.tags="@BUG-003"       # just one
```

| Id | Severity | Summary | Scenario |
| --- | --- | --- | --- |
| [BUG-001](#bug-001) | High | Any customer can edit or delete any other customer's account | `authorization.feature` |
| [BUG-002](#bug-002) | Medium | Any customer can list every registered account | `authorization.feature` |
| [BUG-003](#bug-003) | High | Changing the password through the update endpoint stores it in plain text and locks the account | `authorization.feature` |
| [BUG-004](#bug-004) | High | Any customer can create, edit and delete products | `authorization.feature` |
| [BUG-005](#bug-005) | Low | Registration validation errors return 500 instead of 400 | `authentication.feature` |
| [BUG-006](#bug-006) | Medium | The 12-character password limit is not enforced | `authentication.feature` |
| [BUG-007](#bug-007) | Medium | An account can be created without a password, and can never log in | `authentication.feature` |
| [BUG-008](#bug-008) | High | Stored XSS through product names | `checkout.feature` |
| [BUG-009](#bug-009) | Medium | Updating a product skips validation: negative prices are accepted | `authorization.feature` |
| [BUG-010](#bug-010) | Low | Malformed ids return 500 instead of 400/404 | `authorization.feature` |

---

## BUG-001

**Any logged-in customer can edit or delete any other customer's account.**

`PUT /api/users/update/:id` and `DELETE /api/users/destroyed/:id` check that the request
carries a valid token, but not that the token belongs to the account in the URL.

**Steps**
1. Register and log in as Alice and as Bob.
2. As Bob: `DELETE /api/users/destroyed/<Alice's id>` with Bob's token.

**Expected:** 403. **Actual:** 201 "El usuario fue borrado exitosamente". Alice's account is gone.

**Cause:** `apps/api/src/rutas/userRoute.js` only applies `verifyTokenMiddeleware`; the
controllers never compare `req.user.userId` with `req.params.id`.

**Fix:** reject with 403 when `req.user.userId !== req.params.id`.

## BUG-002

**Any logged-in customer can download the list of every account** (name, surname, age,
degree and email) with `GET /api/users/getAll`.

**Expected:** 403 for a regular customer. **Actual:** 200 with all users.

**Fix:** restrict the route to an administrator role, or remove it.

## BUG-003

**Changing the password with `PUT /api/users/update/:id` stores it in plain text, and the
account can no longer log in.**

**Steps**
1. Register and log in as Alice.
2. `PUT /api/users/update/<Alice's id>` with `{ "password": "NewPass123" }`.
3. `POST /api/users/login` with the new password.

**Expected:** login succeeds. **Actual:** 400 "El email y/o contraseña son incorrectos".

**Cause:** the update uses `findByIdAndUpdate`, which does not run the `pre("save")` hook that
hashes the password, nor the schema validators. The new password is saved as typed, and
`bcrypt.compareSync` cannot match a password against a value that is not a hash.

**Fix:** load the document, assign the fields and call `save()`, or add a dedicated
change-password endpoint that asks for the current password.

## BUG-004

**There are no roles: any registered customer can create, edit and delete products and
categories.** Registration is open to anyone, so in practice anyone can edit the shop.

**Expected:** 403 for a customer account. **Actual:** product created.

**Fix:** add a role to the user model and check it on the write routes of products and
categories. This also closes the entry point of BUG-008.

## BUG-005

**Registration validation errors return 500.** A weak password (`"abc"`), or a missing
required field, answers `500 { "error": "No se puede encontrar los usuarios" }`.

**Expected:** 400 with the reason. **Cause:** `userController.create` catches the Mongoose
`ValidationError` together with real server errors.

## BUG-006

**Passwords longer than 12 characters are accepted**, although the rule (and the hint on the
registration form) says 6 to 12.

**Cause:** the regex in `apps/api/src/utils/validators.js` is not anchored:
`/(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,12}/` only needs 6 characters *somewhere* in the string.

**Fix:** decide the policy first. Anchoring it (`^...{6,12}$`) enforces the documented rule;
removing the upper limit (and updating the hint) is the better policy, since long
passwords are safer.

## BUG-007

**An account can be registered without a password, and that account can never log in.**

**Steps:** `POST /api/users/create` with every field except `password`.

**Expected:** 400. **Actual:** 200, account created. Logging in with that email then returns
500, because `bcrypt.compareSync` throws on an undefined hash.

**Cause:** `password` is not `required` in `userModel.js`, and Mongoose does not run a
custom validator on a missing value.

## BUG-008

**Stored XSS: product names are inserted into the page as HTML.**

`apps/web/js/main.js` and `apps/web/js/carrito.js` build the product cards with template
strings assigned to `innerHTML`. A product named
`<img src=x onerror=...>` runs that script for every visitor of the store.

**Impact:** combined with BUG-004, any visitor can register, create such a product, and run
code in every other visitor's browser — including reading the session token, which lives
in `localStorage`.

**Expected:** the name is shown as text. **Fix:** build the cards with `textContent` /
`createElement`, or escape the values before inserting them.

## BUG-009

**`PUT /api/products/:id` skips the schema validators**: a negative price (or an invalid
status) is saved.

**Expected:** 400. **Actual:** 201 with the negative price.

**Cause:** `findByIdAndUpdate` without `{ runValidators: true }`. A negative price would then
make every order for that product fail with a 500, because the order schema does validate
`min: 0`.

## BUG-010

**Malformed ids return 500.** `DELETE /api/products/not-a-valid-id` (and the same in the user
routes) answers 500 "Error interno en el server".

**Expected:** 400 or 404. **Cause:** Mongoose throws a `CastError` that the controller treats
as a server error. The orders controller already does this right with
`mongoose.isValidObjectId`; the same check fits here.

---

## Other observations (no scenario)

- `POST /api/products` answers 200 instead of 201; `PUT` and `DELETE` answer 201 instead of
  200/204.
- Wrong credentials on login answer 400; 401 is the usual status for a failed login.
- Emails are compared case-sensitively and are not a unique index, so `Ana@x.com` and
  `ana@x.com` are two accounts, and two simultaneous registrations with the same email can
  both succeed.
