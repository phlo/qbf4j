#!/usr/bin/env python

import sys
import json

def table (header, data, align=None):
    assert(not data or len(header) == len(data[0]))
    # markdown string
    md = ""
    # determine column width
    widths = \
        [
            max(
                len(str(header[col])),
                max(len(str(data[row][col])) for row in range(len(data)))
            )
            if data else len(str(header[col]))
            for col in range(len(header))
        ]
    # print header
    md = "| " + " | ".join(h.ljust(w) for h, w in zip(header, widths)) + " |\n"
    # print separator
    seperators = [ "-" * w for w in widths ]
    if align is not None:
        for i in range(len(header)):
            if align[i] == "center" or align[i] == "left":
                seperators[i] = ':' + seperators[i][1:]
            if align[i] == "center" or align[i] == "right":
                seperators[i] = seperators[i][:-1] + ':'
    md = md + "| " + " | ".join(seperators) + " |\n"
    # print table
    for row in data:
        md = md + \
            "| " + \
            " | ".join(
                str(f).ljust(w)
                if f is not None else "".ljust(w)
                for f, w in zip(row, widths)
            ) + \
            " |\n"
    return md

# main
if len(sys.argv) > 1:
    with open(sys.argv[1], 'r') as f:

        def getSimpleName(cls):
            return ".".join(cls.split(".")[-2:])

        results = json.load(f)

        benchmarks = set()
        instances = {}

        # TODO: remove check - obsolete in new version
        if "system" in results[0]["params"]:
            print("# " + results[0]["params"]["system"] + "\n")

        for r in results:
            instance = r["params"]["instance"]
            benchmark = getSimpleName(r["benchmark"])
            score = r["primaryMetric"]["score"]
            error = r["primaryMetric"]["scoreError"]
            result = (score, error if error != "NaN" else 0)

            benchmarks.add(benchmark)

            if instance in instances:
                instances[instance][benchmark] = result
            else:
                instances[instance] = { benchmark: result }

        benchmarks = sorted(benchmarks)

        print(table(
            ["instance"] + benchmarks,
            [
                [i] + [
                    "{0:.2f} ± {1:.2f}".format(s, e)
                    for s, e in [
                        instances[i][b] for b in benchmarks
                    ]
                ]
                for i in sorted(instances.keys())
            ]
        ), end="")
