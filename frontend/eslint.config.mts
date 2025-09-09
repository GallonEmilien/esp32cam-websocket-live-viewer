import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";
import { defineConfig } from "eslint/config";

export default defineConfig([
  {
    files: ["**/*.{js,mjs,cjs,ts,mts,cts}"],
    plugins: { js },
    extends: ["js/recommended"],
    languageOptions: { globals: globals.browser },
    rules: {
      "indent": ["error", 2],
      "no-trailing-spaces": "error",
      "space-before-function-paren": ["error", "never"],
      "space-in-parens": ["error", "never"],
      "comma-dangle": ["error", "always-multiline"],
      "semi": ["error", "always"],
      "object-curly-newline": ["error", { "multiline": true, "consistent": true }],
      "array-bracket-newline": ["error", { "multiline": true, "minItems": 2 }],
      "quotes": ["error", "single"],
      "max-len": ["error", { "code": 80 }],
    },
  },
  tseslint.configs.recommended,
]);
