#!/bin/bash
# Runs trec_eval on every file in results/ and prints one table row per run.
#
# Usage: ./eval.sh [path/to/trec_eval] [qrels.txt]
# Optional: set REL_LEVEL=2 to count only original grades 1-3 as relevant (converted 4,3,2).

TREC=${1:-trec_eval}
QRELS=${2:-qrels.txt}
LEVEL=${REL_LEVEL:-1}

printf "%-32s %8s %10s %8s\n" "run" "MAP" "recall@50" "P@5"
for f in results/*.txt; do
  out=$("$TREC" -l "$LEVEL" -m map -m recall.50 -m P.5 "$QRELS" "$f")
  map=$(echo "$out" | awk '$1=="map" && $2=="all" {print $3}')
  rec=$(echo "$out" | awk '$1=="recall_50" && $2=="all" {print $3}')
  p5=$(echo "$out"  | awk '$1=="P_5" && $2=="all" {print $3}')
  printf "%-32s %8s %10s %8s\n" "$(basename "$f" .txt)" "$map" "$rec" "$p5"
done
