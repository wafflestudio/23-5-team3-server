# @repomix/tree-sitter-wasms

Prebuilt WASM binaries for tree-sitter's language parsers.

Forked from [tree-sitter-wasms](https://github.com/Gregoor/tree-sitter-wasms) to include only the languages needed for Repomix.

**NPM Package**: https://www.npmjs.com/package/@repomix/tree-sitter-wasms

## Installation

```bash
pnpm add @repomix/tree-sitter-wasms
# or
yarn add @repomix/tree-sitter-wasms
# or
npm install @repomix/tree-sitter-wasms
```

## Usage

```ts
import treeSitterRust from "@repomix/tree-sitter-wasms/out/tree-sitter-rust.wasm"
parser.setLanguage(treeSitterCpp);
```

## Supported Languages

This package includes WASM binaries for tree-sitter language parsers listed in [package.json](./package.json) `devDependencies` (packages starting with `tree-sitter-`).

You can also browse all available WASM files at https://unpkg.com/browse/@repomix/tree-sitter-wasms@latest/out/
