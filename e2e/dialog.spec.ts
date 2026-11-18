import { test, expect } from "@playwright/test";
import { gotoGame, getState, stepKey, pressAccept } from "./helpers";

test("sign at (6,9) opens dialog and Space advances", async ({ page }) => {
  await gotoGame(page);
  await stepKey(page, "ArrowLeft", 4);
  const pos = await getState(page);
  expect(pos.currentMap).toBe("akari-village");
  expect(pos.player.x).toBe(6);
  const dlg = page.locator("#dialog");
  await expect(dlg).toBeVisible({ timeout: 5000 });
  await pressAccept(page);
  await expect(page.locator("#dlg-name")).toContainText("看板");
  await expect(page.locator("#dlg-text")).toContainText("灯ノ里");
  await pressAccept(page);
  await expect(dlg).toBeHidden({ timeout: 3000 });
});