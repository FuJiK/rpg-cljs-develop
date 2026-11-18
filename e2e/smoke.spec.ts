import { test, expect } from "@playwright/test";
import { gotoGame, getState } from "./helpers";

test.describe("smoke", () => {
  test("loads akari-village and exposes test hook", async ({ page }) => {
    await gotoGame(page);
    const st = await getState(page);
    expect(st.currentMap).toBe("akari-village");
    expect(st.player.x).toBe(10);
    expect(st.player.y).toBe(9);
    expect(st.mode).toBe("field");
  });
});