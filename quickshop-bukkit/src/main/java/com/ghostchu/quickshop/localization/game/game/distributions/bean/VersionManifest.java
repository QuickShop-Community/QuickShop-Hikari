/*
 *  This file is a part of project QuickShop, the name is VersionManifest.java
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

package com.ghostchu.quickshop.localization.game.game.distributions.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VersionManifest {

    @JsonProperty("latest")
    private LatestDTO latest;
    @JsonProperty("versions")
    private List<VersionsDTO> versions;

    @NoArgsConstructor
    @Data
    public static class LatestDTO {
        @JsonProperty("release")
        private String release;
        @JsonProperty("snapshot")
        private String snapshot;
    }

    @NoArgsConstructor
    @Data
    public static class VersionsDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("type")
        private String type;
        @JsonProperty("url")
        private String url;
        @JsonProperty("time")
        private String time;
        @JsonProperty("releaseTime")
        private String releaseTime;
    }
}
