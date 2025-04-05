package com.cv.s2004orgservice.service.intrface;

import com.cv.s10coreservice.service.intrface.generic.GenericService;
import com.cv.s2002orgservicepojo.dto.UserDetailDto;

public interface UserDetailService extends GenericService<UserDetailDto> {

    Long getCount() throws Exception;
}
