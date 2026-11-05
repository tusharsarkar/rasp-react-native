#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(RaspReactNative, RCTEventEmitter)

RCT_EXTERN_METHOD(startRasp : (NSDictionary *)config)

@end
