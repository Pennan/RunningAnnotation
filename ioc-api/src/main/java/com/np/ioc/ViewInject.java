package com.np.ioc;


public interface ViewInject<T> {
    void inject(T host, Object source);
}
