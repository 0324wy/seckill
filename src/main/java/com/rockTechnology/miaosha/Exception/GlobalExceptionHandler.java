package com.rockTechnology.miaosha.Exception;

import com.rockTechnology.miaosha.controller.LoginController;
import com.rockTechnology.miaosha.result.CodeMsg;
import com.rockTechnology.miaosha.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用于从浏览器拿到异常信息，处理后返回给浏览器
 */

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    //拦截异常的类型
    @ExceptionHandler(value = Exception.class)
    //系统传来的request, exception
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        //todo intanceof, bindexception?
        e.printStackTrace();
        if (e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCodeMsg());
        }else if (e instanceof BindException){
            BindException ex = (BindException) e;
            //从异常中提取信息
            List<ObjectError> allErrors = ex.getAllErrors();
            ObjectError error = allErrors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else {
            log.info(e.getMessage());
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
