package com.shrfid.api.http.auth

import com.twitter.finatra.request.{Header, RouteParam}

// Created by kuangyuan 5/18/2017
case class DeleteAuthUserRequest(@Header Authorization: String,
                                 @RouteParam id: Int)