/*
 *  This file is a part of project QuickShop, the name is ResultContainer.java
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

package com.ghostchu.quickshop.util.envcheck;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResultContainer {
    private final CheckResult result;
    private String resultMessage;

    public ResultContainer(@NotNull CheckResult result, @Nullable String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;
        if (StringUtils.isEmpty(this.resultMessage)) {
            this.resultMessage = "null";
        }
    }

    public CheckResult getResult() {
        return result;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}

