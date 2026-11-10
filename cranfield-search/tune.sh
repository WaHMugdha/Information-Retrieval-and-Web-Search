#!/bin/bash
for k1 in 0.9 1.2 1.5 2.0; do
  for b in 0.4 0.6 0.75 0.9; do
    mvn -q exec:java -Dexec.mainClass=com.cranfield.Main -Dexec.args="batch english bm25:${k1}:${b}"
  done
done
