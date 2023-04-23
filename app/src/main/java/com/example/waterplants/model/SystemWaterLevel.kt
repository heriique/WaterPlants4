package com.example.waterplants.model

data class SystemWaterLevel(var level: Int?) {
    override fun toString(): String {
        if (level != null)
            return "Water level: $level"
        else return "Water level: ..."
    }
}