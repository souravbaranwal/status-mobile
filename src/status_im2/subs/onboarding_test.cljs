(ns status-im2.subs.onboarding-test
  (:require [status-im2.subs.onboarding :as onboarding]
            [cljs.test :refer [deftest is testing]]))

(deftest login-ma-keycard-pairing-test
  (testing "returns nil when no :multiaccounts/login"
    (is (nil? (onboarding/login-ma-keycard-pairing
               {:multiaccounts/login         nil
                :multiaccounts/multiaccounts
                {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
               {}))))

  (testing "returns :keycard-pairing when :multiaccounts/login is present"
    (is (= "keycard-pairing-code"
           (onboarding/login-ma-keycard-pairing
            {:multiaccounts/login         {:key-uid "0x1"}
             :multiaccounts/multiaccounts
             {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
            {})))))
