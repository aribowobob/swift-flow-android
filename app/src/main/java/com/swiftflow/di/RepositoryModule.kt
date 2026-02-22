package com.swiftflow.di

import com.swiftflow.data.repository.AuthRepositoryImpl
import com.swiftflow.data.repository.ChatRepositoryImpl
import com.swiftflow.data.repository.DeliveryRepositoryImpl
import com.swiftflow.data.repository.GeocodingRepositoryImpl
import com.swiftflow.data.repository.ProductRepositoryImpl
import com.swiftflow.domain.repository.AuthRepository
import com.swiftflow.domain.repository.ChatRepository
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.domain.repository.GeocodingRepository
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

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        geocodingRepositoryImpl: GeocodingRepositoryImpl
    ): GeocodingRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
}
