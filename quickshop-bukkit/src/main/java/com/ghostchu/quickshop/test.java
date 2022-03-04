/*
 *  This file is a part of project QuickShop, the name is test.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public class test {
    public static void a (Connection connection) throws SQLException {
        // 随机打印100个随机数，要求必须可以被 2 整除
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int num = random.nextInt(100);
            if (num % 2 == 0) {
                System.out.println(num);
            }
        }
    }
}
