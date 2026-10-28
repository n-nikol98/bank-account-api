package com.nedko.bank.account.mapper;

import com.nedko.bank.account.dto.AccountDto;
import com.nedko.bank.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "currency", source = "currency.code")
    AccountDto toDto(Account account);
}
