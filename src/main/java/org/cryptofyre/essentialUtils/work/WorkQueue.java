package org.cryptofyre.essentialUtils.work;

import java.util.ArrayDeque;
import java.util.Deque;

public class WorkQueue {
    private final Deque<WorkItem> q = new ArrayDeque<>();
    public void add(WorkItem wi) { q.addLast(wi); }
    public WorkItem poll() { return q.pollFirst(); }
    public int size() { return q.size(); }
    public boolean isEmpty() { return q.isEmpty(); }
    public void clear() { q.clear(); }
}
