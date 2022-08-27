package com.ghostchu.quickshop.api.operation;

/**
 * The transaction Operation
 */
public interface Operation {
    /**
     * Commit the operation
     *
     * @return true if successes
     */
    boolean commit();

    /**
     * Check if operation is committed
     *
     * @return true if committed
     */
    boolean isCommitted();

    /**
     * Check if operation is rolled back
     *
     * @return true if rolled back
     */
    boolean isRollback();

    /**
     * Rollback the operation
     *
     * @return true if successes
     */

    boolean rollback();
}
