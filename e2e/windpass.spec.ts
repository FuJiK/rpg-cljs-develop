import { test, expect } from "@playwright/test";
import { gotoGame, getState, stepKey, pressAccept } from "./helpers";

test("north gate warps to windpass", async ({ page }) => {
  await gotoGame(page);
  await stepKey(page, "ArrowUp", 6);
  await expect(page.locator("#dialog")).toBeVisible({ timeout: 5000 });
  await pressAccept(page);
  await pressAccept(page);
  await pressAccept(page);
  await page.waitForFunction(
    () =>
      (window as unknown as { __rpg?: { getState: () => { currentMap: string } } }).__rpg
        ?.getState?.().currentMap === "windpass",
    { timeout: 10_000 }
  );
  const st = await getState(page);
  expect(st.currentMap).toBe("windpass");
  expect(st.player.y).toBe(26);
});