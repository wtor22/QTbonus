package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.BonusValue;
import com.quartztop.bonus.orders.BonusValueDto;
import org.springframework.stereotype.Service;

@Service
public class BonusValueCrudService {

    public static BonusValueDto mapToDto(BonusValue bonusValue) {

        BonusValueDto bonusValueDto = new BonusValueDto();

        bonusValueDto.setValue(bonusValue.getValue());
        bonusValueDto.setId(bonusValue.getId());
        bonusValueDto.setName(bonusValue.getName());
        bonusValueDto.setDescription(bonusValue.getDescription());
        bonusValueDto.setStartDate(bonusValue.getStartDate());
        bonusValueDto.setEndDate(bonusValue.getEndDate());

        return bonusValueDto;
    }

}
