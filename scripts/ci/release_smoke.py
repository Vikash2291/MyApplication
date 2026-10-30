#!/usr/bin/env python3
"""Release (R8-minified, release-signed) smoke test on an emulator via adb + uiautomator.

Installs the APK, then as a guest plays the daily quiz to the result screen, rotates the
screen mid-quiz, returns Home, runs a short monkey pass, and fails on any crash/ANR of the app.
Usage: release_smoke.py <path-to-release.apk>
"""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = "com.alfred.myapplication"


def adb(*args, check=True, timeout=120):
    r = subprocess.run(["adb", *args], capture_output=True, text=True, timeout=timeout)
    if check and r.returncode != 0:
        raise RuntimeError(f"adb {' '.join(args)} failed: {r.stdout}{r.stderr}")
    return r.stdout


def dump():
    for _ in range(5):
        out = adb("exec-out", "uiautomator", "dump", "/dev/tty", check=False)
        m = re.search(r"<\?xml.*</hierarchy>", out, re.S)
        if m:
            return ET.fromstring(m.group(0))
        time.sleep(1)
    raise RuntimeError("uiautomator dump failed: " + out[-500:])


def nodes(root):
    for n in root.iter("node"):
        yield n


def find(root, pattern, exact=False):
    rx = re.compile(pattern)
    for n in nodes(root):
        for attr in ("text", "content-desc"):
            v = n.get(attr) or ""
            if (exact and rx.fullmatch(v)) or (not exact and v and rx.search(v)):
                return n
    return None


def tap(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
    time.sleep(1.2)


def assert_alive(stage):
    crash = adb("logcat", "-d", "-b", "crash", check=False)
    if PKG in crash:
        print(crash)
        raise SystemExit(f"FAIL: crash detected ({stage})")
    if not adb("shell", "pidof", PKG, check=False).strip():
        raise SystemExit(f"FAIL: app process not running ({stage})")
    print(f"ok: alive after {stage}")


def wait_for(pattern, timeout=60, exact=False):
    end = time.time() + timeout
    while time.time() < end:
        root = dump()
        n = find(root, pattern, exact)
        if n is not None:
            return n, root
        # Dismiss a consent / system dialog if one is in the way.
        for p in (r"^(Consent|Agree|Accept|OK|Close|Allow)$",):
            d = find(root, p)
            if d is not None and find(root, pattern) is None:
                tap(d)
        time.sleep(1)
    texts = [n.get("text") for n in nodes(dump()) if n.get("text")]
    raise SystemExit(f"FAIL: '{pattern}' not found. Screen texts: {texts}")


def play_quiz(rotate_mid_quiz=False):
    answered = 0
    while answered < 20:
        root = dump()
        if find(root, r"^Back to Home$", exact=True) is not None:
            return answered
        nxt = find(root, r"^(Next question|See result)$", exact=True)
        if nxt is not None:
            tap(nxt)
            answered += 1
            if rotate_mid_quiz and answered == 3:
                adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
                adb("shell", "settings", "put", "system", "user_rotation", "1")
                time.sleep(3)
                assert_alive("rotation to landscape mid-quiz")
                adb("shell", "settings", "put", "system", "user_rotation", "0")
                time.sleep(3)
                assert_alive("rotation back to portrait")
            continue
        opt = find(root, r"^A$", exact=True)
        if opt is not None:
            tap(opt)
            continue
        time.sleep(1)
    raise SystemExit("FAIL: quiz did not reach the result screen")


def main():
    apk = sys.argv[1]
    adb("logcat", "-c", check=False)
    adb("install", "-r", "-g", apk, timeout=300)
    adb("shell", "monkey", "-p", PKG, "-c", "android.intent.category.LAUNCHER", "1")
    time.sleep(8)
    assert_alive("cold start")

    n, _ = wait_for(r"Continue without an account|Play today|Practice again")
    if "Continue" in (n.get("text") or ""):
        tap(n)
    n, _ = wait_for(r"Play today|Practice again")
    tap(n)
    count = play_quiz(rotate_mid_quiz=True)
    print(f"ok: answered {count} questions, result screen shown")
    assert_alive("quiz finished")
    wait_for(r"Your result")
    n, _ = wait_for(r"^Back to Home$", exact=True)
    tap(n)
    wait_for(r"Play today|Practice again")
    assert_alive("back to Home")

    # Second quiz from Home -> exercises Result -> Home interstitial pacing path too.
    n, _ = wait_for(r"Play today|Practice again")
    tap(n)
    play_quiz()
    n, _ = wait_for(r"^Back to Home$", exact=True)
    tap(n)
    time.sleep(5)
    adb("shell", "input", "keyevent", "KEYCODE_BACK")  # dismiss interstitial if one showed
    time.sleep(2)
    adb("shell", "monkey", "-p", PKG, "-c", "android.intent.category.LAUNCHER", "1")
    time.sleep(3)
    assert_alive("second quiz + interstitial break")

    # Short random-input pass, restricted to the app, no system keys.
    out = subprocess.run(
        ["adb", "shell", "monkey", "-p", PKG, "--pct-syskeys", "0", "--throttle", "150",
         "--ignore-security-exceptions", "-s", "42", "-v", "800"],
        capture_output=True, text=True, timeout=600,
    ).stdout
    if "// CRASH" in out or "// NOT RESPONDING" in out:
        print(out[-4000:])
        raise SystemExit("FAIL: monkey found a crash/ANR")
    crash = adb("logcat", "-d", "-b", "crash", check=False)
    if PKG in crash:
        print(crash)
        raise SystemExit("FAIL: crash in crash buffer")
    print("PASS: release smoke test")


if __name__ == "__main__":
    main()
