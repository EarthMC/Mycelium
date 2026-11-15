// @ts-check
import withNuxt from "./.nuxt/eslint.config.mjs";
import betterTailwindcss from "eslint-plugin-better-tailwindcss";
import { getDefaultSelectors } from "eslint-plugin-better-tailwindcss/api/defaults";
import stylistic from "@stylistic/eslint-plugin";

export default withNuxt(
  stylistic.configs.customize({
    indent: 2,
    quotes: "double",
    semi: true,
    commaDangle: "always-multiline",
  }),

  {
    rules: {
      "@stylistic/linebreak-style": ["error", "unix"],
      "vue/no-multiple-template-root": "off",
      "vue/max-attributes-per-line": ["error", { singleline: 3 }],
    },
  },
  betterTailwindcss.configs["correctness-error"],
  {
    settings: {
      "better-tailwindcss": {
        entryPoint: "app/assets/css/main.css",
        selectors: [
          ...getDefaultSelectors(),
          {
            kind: "attribute",
            name: "^v-bind:ui$",
            match: [{ type: "objectValues" }],
          },
        ],
      },
    },
  },
);
