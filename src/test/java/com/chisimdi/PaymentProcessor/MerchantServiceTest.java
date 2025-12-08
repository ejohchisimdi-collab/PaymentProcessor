package com.chisimdi.PaymentProcessor;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.MerchantSetting;
import com.chisimdi.PaymentProcessor.models.MerchantSettingDTO;
import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.repository.MerchantAccountRepository;
import com.chisimdi.PaymentProcessor.repository.MerchantSettingRepository;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import com.chisimdi.PaymentProcessor.services.MerchantService;
import com.chisimdi.PaymentProcessor.utils.NewMerchantSettingUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MerchantServiceTest {
    @Mock
    MerchantSettingRepository merchantSettingRepository;
    @Mock
    MerchantAccountRepository merchantAccountRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    MerchantService merchantService;

    @Test
    void createMerchantSettingTest() {
        User user = new User();
        MerchantSetting merchantSetting = new MerchantSetting();
        when(userRepository.findByIdAndRole(1, "Merchant")).thenReturn(user);
        when(merchantSettingRepository.save(merchantSetting)).thenReturn(merchantSetting);
        MerchantSettingDTO merchantSettingDTO = merchantService.createMerchantSetting(1, merchantSetting);

        assertThat(merchantSettingDTO.getId()).isEqualTo(merchantSetting.getId());
        assertThat(merchantSettingDTO.getMerchantEndpoint()).isEqualTo(merchantSetting.getMerchantEndpoint());
        assertThat(merchantSettingDTO.getRefundType()).isEqualTo(merchantSetting.getRefundType());
        assertThat(merchantSettingDTO.getMoneyLimit()).isEqualTo(merchantSetting.getMoneyLimit());
        assertThat(merchantSettingDTO.getCurrency()).isEqualTo(merchantSetting.getCurrency());

        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).save(merchantSetting);
    }

    @Test
    void createMerchantSetting_ThrowResourceNotFoundException() {
        MerchantSetting merchantSetting = new MerchantSetting();
        when(userRepository.findByIdAndRole(1, "Merchant")).thenReturn(null);
        assertThatThrownBy(() -> merchantService.createMerchantSetting(1, merchantSetting)).isInstanceOf(ResourceNotFoundException.class);


        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository, never()).save(merchantSetting);
    }

    @Test
    void updateMerchantSetting(){
        NewMerchantSettingUtil merchantSetting2=new NewMerchantSettingUtil();
        MerchantSetting merchantSetting=new MerchantSetting();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(merchantSettingRepository.save(merchantSetting)).thenReturn(merchantSetting);

        MerchantSettingDTO merchantSettingDTO=merchantService.updateMerchantSetting(1,merchantSetting2);
        assertThat(merchantSettingDTO.getId()).isEqualTo(merchantSetting.getId());
        assertThat(merchantSettingDTO.getMerchantEndpoint()).isEqualTo(merchantSetting.getMerchantEndpoint());
        assertThat(merchantSettingDTO.getRefundType()).isEqualTo(merchantSetting.getRefundType());
        assertThat(merchantSettingDTO.getMoneyLimit()).isEqualTo(merchantSetting.getMoneyLimit());
        assertThat(merchantSettingDTO.getCurrency()).isEqualTo(merchantSetting.getCurrency());

        verify(merchantSettingRepository).findByMerchantId(1);
        verify(merchantSettingRepository).save(merchantSetting);
    }
    @Test
    void updateMerchantSetting_ThrowsResourceNotFoundException(){
        NewMerchantSettingUtil merchantSetting2=new NewMerchantSettingUtil();
        MerchantSetting merchantSetting=new MerchantSetting();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(null);

        assertThatThrownBy(()->merchantService.updateMerchantSetting(1,merchantSetting2)).isInstanceOf(ResourceNotFoundException.class);

        verify(merchantSettingRepository).findByMerchantId(1);
        verify(merchantSettingRepository,never()).save(merchantSetting);
    }
}
