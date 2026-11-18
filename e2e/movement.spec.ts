import { test, expect } from "@playwright/test";
import { gotoGame, getState, stepKey } from "./helpers";

test("arrow keys move the player", async ({ page }) => {
  await gotoGame(page);
  const before = await getState(page);
  await stepKey(page, "ArrowLeft", 2);
  const after = await getState(page);
  expect(after.player.x).toBe(before.player.x - 2);
  expect(after.player.y).toBe(before.player.y);
});