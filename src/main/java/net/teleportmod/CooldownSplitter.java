package net.teleportmod;

public class CooldownSplitter {
    // Метод принимает оставшееся время (мс) и общее время отката (сек), возвращая нужный цвет
    public static String getColor(long timeLeftMs, long totalCooldownSec) {
        long totalMs = totalCooldownSec * 1000;
        if (totalMs <= 0) return "§2";
        
        double percentLeft = (double) timeLeftMs / totalMs;

        if (percentLeft > 0.75) return "§4"; // Стадия 1: Больше 75% времени (Темно-красный)
        if (percentLeft > 0.50) return "§6"; // Стадия 2: От 50% до 75% (Оранжевый)
        if (percentLeft > 0.25) return "§e"; // Стадия 3: От 25% до 50% (Желтый)
        return "§2";                        // Стадия 4: Меньше 25% времени (Зеленый)
    }
}
