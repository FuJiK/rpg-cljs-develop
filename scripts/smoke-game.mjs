import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname, resolve, sep } from 'node:path';
import { chromium } from 'playwright';

const root = resolve(process.argv[2] ?? 'dist-game');
const contentTypes = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript',
  '.edn': 'application/edn',
};
const server = createServer(async (req, res) => {
  try {
    const pathname = new URL(req.url, 'http://localhost').pathname;
    const file = resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname));
    if (!file.startsWith(root + sep)) throw new Error('Path outside game directory');
    const data = await readFile(file);
    res.writeHead(200, { 'Content-Type': contentTypes[extname(file)] ?? 'application/octet-stream' });
    res.end(data);
  } catch {
    res.writeHead(404).end();
  }
});

let browser;
try {
  await new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(0, '127.0.0.1', resolve);
  });
  browser = await chromium.launch({
    executablePath: process.env.CHROMIUM_EXECUTABLE,
  });
  const page = await browser.newPage();
  const origin = 'http://127.0.0.1:' + server.address().port;
  const [mapResponse] = await Promise.all([
    page.waitForResponse(origin + '/data/maps/akari-village.edn'),
    page.goto(origin),
  ]);
  assert.equal(mapResponse.status(), 200);
  await page.locator('#game').waitFor({ state: 'visible' });
  await page.locator('#dialog').waitFor({ state: 'visible' });
  await page.keyboard.press('Space');
  assert.equal(await page.locator('#dlg-text').innerText(), '灯ノ里');
  console.log('Production build loaded 灯ノ里 in Chromium.');
} finally {
  await browser?.close();
  server.close();
}
