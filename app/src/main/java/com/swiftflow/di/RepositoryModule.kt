package com.swiftflow.di

import com.swiftflow.data.repository.AuthRepositoryImpl
import com.swiftflow.data.repository.DeliveryRepositoryImpl
import com.swiftflow.data.repository.ProductRepositoryImpl
import com.swiftflow.domain.repository.AuthRepository
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryRepository(
        deliveryRepositoryImpl: DeliveryRepositoryImpl
    ): DeliveryRepository
}
