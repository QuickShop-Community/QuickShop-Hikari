/*
 * This file is a part of project QuickShop, the name is DatabaseTask.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.database;


import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ToString()
public class DatabaseTask {

    private final static Task EMPTY_TASK = ps -> {
    };
    private final String statement;
    private final Task task;

    public DatabaseTask(String statement, Task task) {
        this.statement = statement;
        this.task = task;
    }

    public DatabaseTask(String statement) {
        this.statement = statement;
        this.task = EMPTY_TASK;
    }


    public void run(@NotNull Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(statement)) { //TODO Use addBatch to improve performance
            task.edit(ps);
            ps.execute();
            task.onSuccess();
        } catch (SQLException e) {
            task.onFailed(e);
        }
    }

    interface Task {
        /**
         * Edit action before commit the PreparedStatement
         *
         * @param ps The PreparedStatement
         * @throws SQLException Throws if any exception throws
         */
        void edit(PreparedStatement ps) throws SQLException;

        /**
         * Calling when PreparedStatement finished without any errors
         */
        default void onSuccess() {
        }

        /**
         * Calling when PreparedStatement failed to commit
         *
         * @param e The exception
         */
        default void onFailed(SQLException e) {
            e.printStackTrace();
        }

    }

}
