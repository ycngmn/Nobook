import { build } from 'esbuild';
import { readdir, mkdir } from 'node:fs/promises';
import { join, dirname, basename, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const SRC_DIR = __dirname;
const OUT_DIR = resolve(__dirname, '../res/raw');

async function listTsFiles(dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  const files = [];
  for (const e of entries) {
    if (e.isDirectory()) {
      if (e.name === 'node_modules' || e.name === 'types') continue;
      files.push(...await listTsFiles(join(dir, e.name)));
    } else if (e.isFile() && e.name.endsWith('.ts') && !e.name.endsWith('.d.ts')) {
      files.push(join(dir, e.name));
    }
  }
  return files;
}

async function main() {
  const tsFiles = await listTsFiles(SRC_DIR);
  if (tsFiles.length === 0) {
    process.stdout.write('[esbuild] No .ts source files to transpile.\n');
    return;
  }
  await mkdir(OUT_DIR, { recursive: true });
  await Promise.all(tsFiles.map((file) => {
    const base = basename(file, '.ts');
    return build({
      entryPoints: [file],
      outfile: join(OUT_DIR, `${base}.js`),
      target: 'es2020',
      platform: 'browser',
      bundle: false,
      format: 'iife',
      sourcemap: false,
      minify: false,
      logLevel: 'warning',
      write: true,
    });
  }));
  process.stdout.write(`[esbuild] Transpiled ${tsFiles.length} TS file(s) -> app/src/main/res/raw/*.js\n`);
}

main().catch((err) => {
  process.stderr.write(`[esbuild] build failed: ${err.message}\n`);
  process.exit(1);
});
