#!/usr/bin/env python3
"""Builds app/src/main/assets/questions.json from the bank_*.py sources.

Run from the repository root:  python3 tools/questions/build_bank.py

- Every question gets a stable id "<category-prefix>-<nnn>" (order inside its list).
  Append new questions at the END of a list so existing ids (bookmarks, progress) stay valid.
- Options are written correct-first in the sources and shuffled here with a seed derived from
  the id, so the correct answer position is spread evenly but stable between builds.
- Bump BANK_VERSION whenever content changes so installed apps re-import the bank.
"""
import hashlib
import json
import os
import random
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from bank_india_world import INDIA_GK, WORLD_GK  # noqa: E402
from bank_science_history import SCIENCE, HISTORY  # noqa: E402
from bank_geo_sports import GEOGRAPHY, SPORTS  # noqa: E402
from bank_current_computers import CURRENT_AFFAIRS, COMPUTERS  # noqa: E402

BANK_VERSION = 1

CATEGORIES = [
    ("INDIA_GK", "ind", INDIA_GK),
    ("WORLD_GK", "wld", WORLD_GK),
    ("SCIENCE", "sci", SCIENCE),
    ("HISTORY", "his", HISTORY),
    ("GEOGRAPHY", "geo", GEOGRAPHY),
    ("SPORTS", "spo", SPORTS),
    ("CURRENT_AFFAIRS", "cur", CURRENT_AFFAIRS),
    ("COMPUTERS", "cmp", COMPUTERS),
]


def fail(msg):
    print("ERROR:", msg, file=sys.stderr)
    sys.exit(1)


def main():
    out = []
    seen_en = set()
    for category, prefix, items in CATEGORIES:
        for i, item in enumerate(items, start=1):
            if len(item) != 6:
                fail(f"{category}#{i}: expected 6 fields")
            en_q, en_opts, en_exp, hi_q, hi_opts, hi_exp = item
            qid = f"{prefix}-{i:03d}"
            for lang, opts in (("en", en_opts), ("hi", hi_opts)):
                if len(opts) != 4:
                    fail(f"{qid}: {lang} needs 4 options")
                if len(set(opts)) != 4:
                    fail(f"{qid}: {lang} options must be distinct")
            for text in (en_q, en_exp, hi_q, hi_exp):
                if not text.strip():
                    fail(f"{qid}: empty text")
            key = en_q.strip().lower()
            if key in seen_en:
                fail(f"{qid}: duplicate question '{en_q}'")
            seen_en.add(key)

            seed = int(hashlib.sha256(qid.encode()).hexdigest()[:8], 16)
            order = [0, 1, 2, 3]
            random.Random(seed).shuffle(order)
            answer = order.index(0)
            out.append({
                "id": qid,
                "category": category,
                "answer": answer,
                "en": {"q": en_q, "options": [en_opts[k] for k in order], "explanation": en_exp},
                "hi": {"q": hi_q, "options": [hi_opts[k] for k in order], "explanation": hi_exp},
            })

    root = os.path.abspath(os.path.join(HERE, "..", ".."))
    path = os.path.join(root, "app", "src", "main", "assets", "questions.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"version": BANK_VERSION, "questions": out}, f, ensure_ascii=False, indent=1)
        f.write("\n")

    counts = {}
    for q in out:
        counts[q["category"]] = counts.get(q["category"], 0) + 1
    dist = [0, 0, 0, 0]
    for q in out:
        dist[q["answer"]] += 1
    print(f"Wrote {len(out)} questions to {path}")
    for c, n in counts.items():
        print(f"  {c:16s} {n}")
    print("  answer position distribution:", dist)


if __name__ == "__main__":
    main()
