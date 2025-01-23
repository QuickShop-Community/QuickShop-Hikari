package com.ghostchu.quickshop.compatibility.towny.compat.tne;

/*
 * QuickShop - Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.tnemc.core.TNECore;
import net.tnemc.core.account.Account;

import java.util.Optional;
import java.util.UUID;

/**
 * TNEConversion
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class TNEConversion implements UuidConversion {

  @Override
  public UUID convertTownyAccount(final Town town) {

    final Optional<Account> account = TNECore.api().getAccount(town.getAccount().getName());
    return account.map(Account::getIdentifier).orElseGet(()->QuickShop.getInstance().getPlayerFinder().name2Uuid(town.getAccount().getName()));
  }

  @Override
  public UUID convertTownyAccount(final Nation nation) {

    final Optional<Account> account = TNECore.api().getAccount(nation.getAccount().getName());
    return account.map(Account::getIdentifier).orElseGet(()->QuickShop.getInstance().getPlayerFinder().name2Uuid(nation.getAccount().getName()));
  }
}
