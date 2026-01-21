'use strict';

const { Node, Block } = require('./Node');
const languages = require('./languages');

const constants = {
  ESCAPED_CHAR_REGEX: /\\./y,
  QUOTED_STRING_REGEX: /(['"`])((?:\\\1|[^\1])+?)(\1)/y,
  NEWLINE_REGEX: /\r*\n/y
};

/**
 * Convert a regex with ^ anchor to a sticky regex
 */
const toStickyRegex = (regex) => {
  if (!regex) return null;
  // Remove ^ anchor from the beginning if present and add sticky flag
  const source = regex.source.replace(/^\^/, '');
  return new RegExp(source, 'y');
};

const parse = (input, options = {}) => {
  if (typeof input !== 'string') {
    throw new TypeError('Expected input to be a string');
  }

  const cst = new Block({ type: 'root', nodes: [] });
  const stack = [cst];
  const name = (options.language || 'javascript').toLowerCase();
  const lang = languages[name];

  if (typeof lang === 'undefined') {
    throw new Error(`Language "${name}" is not supported by strip-comments`);
  }

  const { LINE_REGEX, BLOCK_OPEN_REGEX, BLOCK_CLOSE_REGEX, BLOCK_REQUIRES_LINE_START, NESTED_BLOCK_COMMENTS } = lang;

  // Convert all regexes to sticky versions
  const STICKY_LINE_REGEX = toStickyRegex(LINE_REGEX);
  const STICKY_BLOCK_OPEN_REGEX = toStickyRegex(BLOCK_OPEN_REGEX);
  const STICKY_BLOCK_CLOSE_REGEX = toStickyRegex(BLOCK_CLOSE_REGEX);

  // Build a regex to find the next "interesting" character position
  // This includes: newlines, quote chars, escape char, and comment start chars
  const specialChars = new Set(['\n', '\r', '\\', "'", '"', '`']);

  // Add language-specific chars
  if (BLOCK_OPEN_REGEX) {
    const firstChar = BLOCK_OPEN_REGEX.source.replace(/^\^/, '')[0];
    if (firstChar && firstChar !== '\\' && firstChar !== '[') {
      specialChars.add(firstChar);
    }
  }
  if (LINE_REGEX) {
    const firstChar = LINE_REGEX.source.replace(/^\^/, '')[0];
    if (firstChar && firstChar !== '\\' && firstChar !== '[') {
      specialChars.add(firstChar);
    }
  }

  // For JavaScript-like languages, add / and *
  specialChars.add('/');
  specialChars.add('*');
  specialChars.add('-'); // for HTML, Lua, Ada, etc.
  specialChars.add('#'); // for Python, Ruby, Perl, etc.
  specialChars.add('('); // for AppleScript, Pascal
  specialChars.add('{'); // for Haskell
  specialChars.add('%'); // for MATLAB
  specialChars.add('='); // for Ruby
  specialChars.add(']'); // for Lua
  specialChars.add('⍝'); // for APL
  specialChars.add('<'); // for HTML
  specialChars.add('>'); // for HTML close

  // Create regex to skip to next special char
  // Note: - must be at the end or escaped in character class
  const escapedChars = [...specialChars].map(c => {
    if (c === '-') return '\\-';
    if (c === ']') return '\\]';
    if (c === '\\') return '\\\\';
    if (c === '^') return '\\^';
    return c.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }).join('');
  const SKIP_REGEX = new RegExp(`[^${escapedChars}]+`, 'y');

  let block = cst;
  let pos = 0;
  const len = input.length;
  let token;
  let prev;
  let isAtLineStart = true; // Track if we're at the start of a line (only whitespace since last newline)

  const source = [BLOCK_OPEN_REGEX, BLOCK_CLOSE_REGEX].filter(Boolean);
  let tripleQuotes = false;

  // Check if this is a Python-style triple-quote language (supports """ and/or ''')
  if (source.length > 0 && source.every(regex => /"""/.test(regex.source) || /'''/.test(regex.source))) {
    tripleQuotes = true;
  }

  /**
   * Helpers
   */

  const scan = (regex, type = 'text') => {
    if (!regex) return null;
    regex.lastIndex = pos;
    const match = regex.exec(input);
    if (match) {
      pos += match[0].length;
      return { type, value: match[0], match };
    }
    return null;
  };

  const push = node => {
    if (prev && prev.type === 'text' && node.type === 'text') {
      prev.value += node.value;
      return;
    }
    block.push(node);
    if (node.nodes) {
      stack.push(node);
      block = node;
    }
    prev = node;
  };

  const pop = () => {
    if (block.type === 'root') {
      throw new SyntaxError('Unclosed block comment');
    }
    stack.pop();
    block = stack[stack.length - 1];
  };

  /**
   * Parse input string
   */

  while (pos < len) {
    // Try to skip large chunks of plain text at once
    SKIP_REGEX.lastIndex = pos;
    const skipMatch = SKIP_REGEX.exec(input);
    if (skipMatch && skipMatch[0].length > 0) {
      push(new Node({ type: 'text', value: skipMatch[0] }));
      // Update isAtLineStart: if skipped text contains non-whitespace, we're no longer at line start
      if (!/^\s*$/.test(skipMatch[0])) {
        isAtLineStart = false;
      }
      pos += skipMatch[0].length;
      if (pos >= len) break;
    }

    // escaped characters
    if ((token = scan(constants.ESCAPED_CHAR_REGEX, 'text'))) {
      push(new Node(token));
      isAtLineStart = false;
      continue;
    }

    // quoted strings
    // Skip triple quotes check for Python-style languages (both """ and ''')
    const atTripleQuote = tripleQuotes && (input.startsWith('"""', pos) || input.startsWith("'''", pos));
    if (block.type !== 'block' && (!prev || !/\w$/.test(prev.value)) && !atTripleQuote) {
      if ((token = scan(constants.QUOTED_STRING_REGEX, 'text'))) {
        push(new Node(token));
        isAtLineStart = false;
        continue;
      }
    }

    // For Python-style languages: handle triple-quoted string literals (not at line start)
    // These should be preserved as-is, not treated as docstrings
    if (tripleQuotes && atTripleQuote && block.type !== 'block') {
      const canMatchBlockOpen = !BLOCK_REQUIRES_LINE_START || isAtLineStart;
      if (!canMatchBlockOpen) {
        // This is a string literal (""" or ''' not at line start), find closing delimiter
        const delimiter = input.slice(pos, pos + 3); // """ or '''
        const closePos = input.indexOf(delimiter, pos + 3);
        if (closePos !== -1) {
          // Include everything from opening to closing delimiter
          const stringLiteral = input.slice(pos, closePos + 3);
          push(new Node({ type: 'text', value: stringLiteral }));
          pos = closePos + 3;
          isAtLineStart = false;
          continue;
        }
        // No closing delimiter found, treat as text and let it continue
      }
    }

    // newlines
    if ((token = scan(constants.NEWLINE_REGEX, 'newline'))) {
      push(new Node(token));
      isAtLineStart = true; // Reset at newline
      continue;
    }

    // block comment open
    // If BLOCK_REQUIRES_LINE_START is set, only match if we're at the start of a line
    // Don't allow nested block comments unless NESTED_BLOCK_COMMENTS is set (e.g., Swift, MATLAB, OCaml)
    const canMatchBlockOpen = !BLOCK_REQUIRES_LINE_START || isAtLineStart;
    const allowNestedBlock = NESTED_BLOCK_COMMENTS || block.type !== 'block';
    if (STICKY_BLOCK_OPEN_REGEX && options.block && canMatchBlockOpen && allowNestedBlock) {
      if ((token = scan(STICKY_BLOCK_OPEN_REGEX, 'open'))) {
        // For triple quotes, store the delimiter to match on close
        const newBlock = new Block({ type: 'block' });
        if (tripleQuotes) {
          newBlock.openDelimiter = token.value;
        }
        push(newBlock);
        push(new Node(token));
        continue;
      }
    }

    // block comment close
    if (STICKY_BLOCK_CLOSE_REGEX && block.type === 'block' && options.block) {
      // For triple quotes, only close if delimiter matches the opener
      if (tripleQuotes && block.openDelimiter) {
        if (input.startsWith(block.openDelimiter, pos)) {
          token = scan(STICKY_BLOCK_CLOSE_REGEX, 'close');
          if (token) {
            token.newline = token.match[1] || '';
            push(new Node(token));
            pop();
            continue;
          }
        }
      } else if ((token = scan(STICKY_BLOCK_CLOSE_REGEX, 'close'))) {
        token.newline = token.match[1] || '';
        push(new Node(token));
        pop();
        continue;
      }
    }

    // line comment
    if (STICKY_LINE_REGEX && block.type !== 'block' && options.line) {
      if ((token = scan(STICKY_LINE_REGEX, 'line'))) {
        push(new Node(token));
        continue;
      }
    }

    // Single character that didn't match anything special
    const char = input[pos];
    push(new Node({ type: 'text', value: char }));
    if (!/\s/.test(char)) {
      isAtLineStart = false;
    }
    pos++;
  }

  return cst;
};

module.exports = parse;
