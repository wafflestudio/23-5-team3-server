/**
 * Options for stripping comments
 */
export interface StripOptions {
  /**
   * The programming language of the input string
   * @default 'javascript'
   */
  language?: string;
  /**
   * If `false`, strip only block comments
   * @default true
   */
  line?: boolean;
  /**
   * If `false`, strip only line comments
   * @default true
   */
  block?: boolean;
  /**
   * Keep protected comments (e.g. `/*!` and `//!`)
   * @default false
   */
  keepProtected?: boolean;
  /**
   * Alias for `keepProtected`
   */
  safe?: boolean;
  /**
   * Preserve newlines after comments are stripped
   * @default false
   */
  preserveNewlines?: boolean;
  /**
   * Strip only the first comment
   * @default false
   */
  first?: boolean;
}

/**
 * A node in the Concrete Syntax Tree (CST)
 */
export interface Node {
  type: string;
  value?: string;
  match?: RegExpExecArray;
  newline: string;
  protected: boolean;
}

/**
 * A block node in the CST, containing child nodes
 */
export interface Block extends Node {
  nodes: (Node | Block)[];
  push(node: Node | Block): void;
}

/**
 * Strip all code comments from the given input string
 * @param input - String from which to strip comments
 * @param options - Options for stripping
 * @returns Modified string with comments removed
 */
declare function strip(input: string, options?: StripOptions): string;

declare namespace strip {
  /**
   * Strip only block comments
   * @param input - String from which to strip comments
   * @param options - Options for stripping
   * @returns Modified string with block comments removed
   */
  function block(input: string, options?: StripOptions): string;

  /**
   * Strip only line comments
   * @param input - String from which to strip comments
   * @param options - Options for stripping
   * @returns Modified string with line comments removed
   */
  function line(input: string, options?: StripOptions): string;

  /**
   * Strip the first comment from the given input
   * @param input - String from which to strip the first comment
   * @param options - Options for stripping
   * @returns Modified string with the first comment removed
   */
  function first(input: string, options?: StripOptions): string;

  /**
   * Parse a string and return a CST (Concrete Syntax Tree)
   * @param input - String to parse
   * @param options - Options for parsing
   * @returns CST representing the parsed input
   */
  function parse(input: string, options?: StripOptions): Block;
}

export = strip;
