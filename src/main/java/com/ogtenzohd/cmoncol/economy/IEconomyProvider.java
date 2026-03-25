package com.ogtenzohd.cmoncol.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public interface IEconomyProvider {

    double getBalance(ServerPlayer player);
    boolean withdraw(ServerPlayer player, double amount);
    void deposit(ServerPlayer player, double amount);
    Component formatCurrency(double amount);
}