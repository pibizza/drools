package org.drools.core.common;


public interface SimpleWorkingMemory {
    

    <T extends Memory> T getNodeMemory(MemoryFactory<T> node);

    NodeMemories getNodeMemories();

}
