import { expect, type Page } from "@playwright/test";

export type GameState = {
  ready: boolean;
  currentMap: string;
  mapName?: string;
  mode: string;
  player: { x: number; y: number; dir?: string };
  dialog: { open?: boolean; queueSize?: number };
  opened: string[];
};

const KEY_SPECS: Record<string, { code: string; keyCode: number }> = {
  ArrowLeft: { code: "ArrowLeft", keyCode: 37 },
  ArrowRight: { code: "ArrowRight", keyCode: 39 },
  ArrowUp: { code: "ArrowUp", keyCode: 38 },
  ArrowDown: { code: "ArrowDown", keyCode: 40 },
  Space: { code: "Space", keyCode: 32 },
};

export async function gotoGame(page: Page) {
  await page.addInitScript(() => {
    const w = window as unknown as { __RPG_E2E__?: boolean };
    w.__RPG_E2E__ = true;
    localStorage.removeItem("rpg.cljs.save.v1");
  });
  await page.goto("/index.html");
  await expect(page.locator("#game")).toBeVisible();
  await page.locator("#game").click();
  await waitForGameReady(page);
  await dismissBlockingDialogs(page);
}

/** マップ入場時の「場所」など、起動直後の会話を片付ける */
export async function dismissBlockingDialogs(page: Page) {
  for (let i = 0; i < 6; i++) {
    const st = await getState(page);
    if (!st.dialog?.open) {
      return;
    }
    await page.locator("#game").click();
    await pressAccept(page);
    await page.waitForTimeout(180);
  }
}

export async function waitForGameReady(page: Page) {
  await page.waitForFunction(
    () => {
      const w = window as unknown as {
        __rpg?: { ready?: boolean; getState?: () => GameState };
      };
      return w.__rpg?.ready === true && w.__rpg?.getState?.()?.currentMap === "akari-village";
    },
    { timeout: 20_000 }
  );
}

export async function getState(page: Page): Promise<GameState> {
  return page.evaluate(() => {
    const w = window as unknown as { __rpg: { getState: () => GameState } };
    return w.__rpg.getState();
  });
}

async function dispatchKey(page: Page, key: string, pressed: boolean) {
  const spec = KEY_SPECS[key] ?? { code: key, keyCode: 0 };
  await page.evaluate(
    ({ key, code, keyCode, pressed }) => {
      const type = pressed ? "keydown" : "keyup";
      window.dispatchEvent(
        new KeyboardEvent(type, {
          key,
          code,
          keyCode,
          which: keyCode,
          bubbles: true,
          cancelable: true,
        })
      );
    },
    { key, code: spec.code, keyCode: spec.keyCode, pressed }
  );
}

/** One tile step (~120ms anim); waits until player grid position changes once. */
export async function stepKey(page: Page, key: string, times = 1) {
  for (let i = 0; i < times; i++) {
    await page.locator("#game").click();
    const { x, y } = (await getState(page)).player;
    await page.keyboard.down(key);
    try {
      await page.waitForFunction(
        ({ x0, y0 }: { x0: number; y0: number }) => {
          const p = (window as unknown as { __rpg: { getState: () => GameState } }).__rpg.getState().player;
          return p.x !== x0 || p.y !== y0;
        },
        { x0: x, y0: y },
        { timeout: 5000 }
      );
    } finally {
      await page.keyboard.up(key);
    }
    await page.waitForTimeout(140);
  }
}

export async function pressAccept(page: Page) {
  await dispatchKey(page, "Space", true);
  await dispatchKey(page, "Space", false);
  await page.waitForTimeout(120);
}