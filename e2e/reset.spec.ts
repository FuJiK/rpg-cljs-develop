import { test, expect } from "@playwright/test";
import { gotoGame, getState, stepKey } from "./helpers";

test("data reset returns to fresh akari-village", async ({ page }) => {
  await gotoGame(page);
  await stepKey(page, "ArrowLeft", 4);
  page.once("dialog", (d) => d.accept());
  await page.locator("#btn-reset").click();
  await page.waitForTimeout(800);
  const st = await getState(page);
  expect(st.currentMap).toBe("akari-village");
  expect(st.player.x).toBe(10);
  expect(st.player.y).toBe(9);
  expect(st.opened).toEqual([]);
});