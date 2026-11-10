#!/bin/bash
# Converts cranqrel ("query doc grade", 1 = best, -1 = no interest)
# into trec_eval format ("query 0 doc rel", higher = more relevant).
#
# Mapping: 1 -> 4, 2 -> 3, 3 -> 2, 4 -> 1, -1 -> 0 (not relevant)
#
# Usage: ./convert_qrels.sh ../cranqrel > qrels.txt
tr -d '\r' < "${1:?usage: $0 path/to/cranqrel}" | awk 'NF >= 3 { r = ($3 == -1) ? 0 : 5 - $3; print $1, 0, $2, r }'
