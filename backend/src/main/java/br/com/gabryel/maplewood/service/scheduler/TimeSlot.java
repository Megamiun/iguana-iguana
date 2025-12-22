package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.Weekday;

public record TimeSlot(Weekday weekday, int slot) { }
