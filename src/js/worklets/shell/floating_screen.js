import { useDerivedValue, withTiming, withSequence, withDelay, Easing } from 'react-native-reanimated';
import * as constants from './constants';

// Derived Values
export function screenLeft (screenState, screenWidth) {
  return useDerivedValue(
    function () {
      'worklet'
      const switcherCardLeftPosition = 25; // Todo find this dynamically
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SLIDE_ANIMATION:
	return withTiming(screenWidth, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SLIDE_ANIMATION:
	return withSequence(withTiming(screenWidth, {duration: 0}), withTiming(0, constants.EASE_OUT_EASING));
	break;
      case constants.CLOSE_SCREEN_WITHOUT_ANIMATION:
	return screenWidth;
	break;
      case constants.OPEN_SCREEN_WITHOUT_ANIMATION:
	return 0;
	break;
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
	return withTiming(switcherCardLeftPosition, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
	return withSequence(withTiming(switcherCardLeftPosition, {duration: 0}), withTiming(0, constants.EASE_OUT_EASING));
	break;
      }
    }
    , []);
}

export function screenTop (screenState) {
  return useDerivedValue(
    function () {
      'worklet'
      const switcherCardTopPosition = 183; // Todo find this dynamically
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
	return withTiming(switcherCardTopPosition, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
	return withSequence(withTiming(switcherCardTopPosition, {duration: 0}), withTiming(0, constants.EASE_OUT_EASING));
	break;
      default:
	return 0;
      }
    }
  );
}

export function screenWidth (screenState, screenWidth, switcherCardSize) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
	return withTiming(switcherCardSize, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
	return withSequence(withTiming(switcherCardSize, {duration: 0}), withTiming(screenWidth, constants.EASE_OUT_EASING));
	break;
      default:
	return screenWidth;
      }
    }
  );
}

export function screenHeight (screenState, screenHeight, switcherCardSize) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
	return withTiming(switcherCardSize, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
	return withSequence(withTiming(switcherCardSize, {duration: 0}), withTiming(screenHeight, constants.EASE_OUT_EASING));
	break;
      default:
	return screenHeight;
      }
    }
  );
}

export function screenZIndex (screenState) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
      case constants.CLOSE_SCREEN_WITH_SLIDE_ANIMATION:
	return withDelay(constants.SHELL_ANIMATION_TIME, withTiming(-1, {duration: 0}));
	break;
      case constants.CLOSE_SCREEN_WITHOUT_ANIMATION:
	return -1;
	break;
      default:
	return 1;
      }
    }
  );
}

export function screenMarginTop (screenState) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
	return withTiming(-200, constants.EASE_OUT_EASING);
	break;
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
	return withSequence(withTiming(-200, {duration: 0}), withTiming(0, constants.EASE_OUT_EASING));
	break;
      default:
	return 0;
      }
    }
  );
}
