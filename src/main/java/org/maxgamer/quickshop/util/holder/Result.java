/*
 * This file is a part of project QuickShop, the name is Result.java
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

package org.maxgamer.quickshop.util.holder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Result {

    public static final Result SUCCESS = new Result() {
        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public void setMessage(String message) {
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public void setResult(boolean result) {
        }

        @Override
        public String getListener() {
            return "";
        }
    };
    @Setter
    private boolean result = false;
    @Setter
    @Getter
    private String message;
    @Getter
    @Setter
    private String listener;


    public Result() {
    }

    public Result(String message) {
        result = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return result;
    }
}
