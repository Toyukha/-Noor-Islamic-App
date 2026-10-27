const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- UNATHENTICATED ACCESS REJECTION ---
test("Unauthenticated user: cannot read user profiles or daily logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("daily_logs").get());
});

// --- CROSS-USER ISOLATION ---
test("Authenticated user: cannot read or write another user's profile or logs", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-10-26").set({
      id: "2026-10-26",
      userId: BOB_UID,
      date: "2026-10-26",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

// --- OWNER SUCCESSFUL OPERATIONS ---
test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      city: "Cairo",
      calculationMethod: "Egyptian",
      currentStreak: 5,
      longestStreak: 12,
      lastActiveDate: "2026-10-26",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(profileRef.get());
});

test("Authenticated user: can create and read their own daily obligation log", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-10-26");
  await assertSucceeds(
    logRef.set({
      id: "2026-10-26",
      userId: ALICE_UID,
      date: "2026-10-26",
      fajr: true,
      dhuhr: true,
      asr: true,
      maghrib: false,
      isha: false,
      sunnahPrayers: 2,
      isFasting: true,
      fastingType: "Sunnah",
      quranPagesRead: 4,
      quranMinutes: 20,
      lastSurah: "Al-Baqarah",
      lastAyah: 255,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(logRef.get());
});
