import sys
import collections
import csv
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.ticker as ticker
from matplotlib.ticker import MaxNLocator

file_name = sys.argv[1]
my_reader = csv.reader(open(file_name))

counts = collections.Counter()
for row in my_reader:
    counts[row[0]] += 1


thread_num = sys.argv[2]
iter_num = sys.argv[3]


fig, ax = plt.subplots()
ax.plot(counts.keys(), counts.values())
start, end = ax.get_xlim()
ax.xaxis.set_major_locator(MaxNLocator(integer=True))
ax.xaxis.set_ticks(np.arange(0, end, int(end/20)))

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(20)

title = plt.title(f'Overall Throughput {thread_num} Threads {iter_num} Iterations' )
title.set_fontsize(30)
plt.xlabel(f'time' )
plt.ylabel(f'throughput in second')
plt.grid()
plt.show()