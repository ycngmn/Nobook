Auth storage for Playwright tests. Regenerate if login expires.

From project root, run this command to generate a session. 
Login with your facebook credentials and close the window.

```
npx playwright codegen https://facebook.com --save-storage=app/src/test/resources/auth.json
``` 