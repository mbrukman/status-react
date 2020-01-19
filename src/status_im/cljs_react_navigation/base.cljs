(ns status-im.cljs-react-navigation.base
  (:require [cljs.spec.alpha :as s :include-macros true]
            [oops.core :refer [oget]]
            [reagent.core :as r]
            [reagent.impl.component :as ric]))

(defonce React (js/require "react"))
(defonce ReactNavigation (js/require "react-navigation"))
(defonce ReactNavigationStack (js/require "react-navigation-stack"))
(defonce ReactNavigationTabs (js/require "react-navigation-tabs"))
(defonce TwoPaneNavigator (js/require "react-native-navigation-twopane"))

(defonce isValidElement (oget React ["isValidElement"]))

;; Core
(defonce createAppContainer (oget ReactNavigation ["createAppContainer"]))
(defonce StateUtils (oget ReactNavigation ["StateUtils"]))

(defonce NavigationEvents (oget ReactNavigation ["NavigationEvents"]))
(defonce NavigationActions (oget ReactNavigation ["NavigationActions"]))

(defonce StackActions (oget ReactNavigation ["StackActions"]))

;; Navigators
(defonce createNavigator (oget ReactNavigation ["createNavigator"]))
(defonce createStackNavigator (oget ReactNavigationStack ["createStackNavigator"]))
(defonce createSwitchNavigator (oget ReactNavigation ["createSwitchNavigator"]))
(defonce createBottomTabNavigator (oget ReactNavigationTabs ["createBottomTabNavigator"]))
(defonce createTwoPaneNavigator (oget TwoPaneNavigator ["createTwoPaneNavigator"]))

;; Views
(defonce StackView (oget ReactNavigationStack ["StackView"]))
(defonce HeaderView (oget ReactNavigationStack ["Header"]))
(defonce SwitchView (oget ReactNavigation ["SwitchView"]))

(assert (and React ReactNavigation ReactNavigationTabs ReactNavigationStack TwoPaneNavigator)
        "React, React Navigation and peer dependencies must be installed.")

;; Spec conforming functions

(defn react-component?
  "Spec conforming function.  Accepts a react class or returns :cljs.spec.alpha/invalid."
  [c]
  (cond
    (ric/react-class? c) c
    :else :cljs.spec.alpha/invalid))

(defn react-element?
  "Spec conforming function.  Accepts either a react element, conforms a react class to an element, or returns :cljs.spec.alpha/invalid."
  [e]
  (cond
    (isValidElement e) e
    (ric/react-class? e) (r/create-element e)
    :else :cljs.spec.alpha/invalid))

(defn fn-or-react-component?
  "Confirms either a valid react component was passed in or a function that returns a react component.
  If it's a function, props will be converted to clojure structures with keywords. Expects the fn to return a valid component"
  [fn-or-c]
  (cond
    (ric/react-class? fn-or-c) fn-or-c
    (fn? fn-or-c) (fn [props & children]
                    (let [clj-props (js->clj props :keywordize-keys true)
                          react-c (fn-or-c clj-props children)]
                      react-c))
    :else :cljs.spec.alpha/invalid))

;; React
(s/def :react/component (s/conformer react-component?))
(s/def :react/element (s/conformer react-element?))

;; RouteConfigs
(s/def :react-navigation.RouteConfigs.route/screen (s/conformer fn-or-react-component?))
(s/def :react-navigation.RouteConfigs.route/path string?)
(s/def :react-navigation.RouteConfigs/route (s/keys :req-un [:react-navigation.RouteConfigs.route/screen]
                                                    :opt-un [:react-navigation.RouteConfigs.route/path
                                                             :react-navigation/navigationOptions]))
(s/def :react-navigation/RouteConfigs (s/map-of keyword? :react-navigation.RouteConfigs/route))

(defn append-navigationOptions
  "If navigationOptions are specified append to the react-component"
  [react-component navigationOptions]
  (when (and navigationOptions (not= navigationOptions :cljs.spec.alpha/invalid))
    (aset react-component "navigationOptions" (clj->js navigationOptions)))
  react-component)

(s/fdef append-navigationOptions
        :args (s/tuple :react/component :react-navigation/navigationOptions)
        :ret #(ric/react-class? %))

(defn stack-screen [react-component]
  (let [react-component-conformed   (s/conform :react/component react-component)]
    (assert (not= react-component-conformed :cljs.spec.alpha/invalid) "Invalid react component.")
    react-component-conformed))

;; Navigators

(defn stack-navigator [routeConfigs stackNavigatorConfig]
  (let [routeConfigs-conformed         (s/conform :react-navigation/RouteConfigs routeConfigs)]
    (assert (not= routeConfigs-conformed :cljs.spec.alpha/invalid))
    (if stackNavigatorConfig
      (createStackNavigator (clj->js routeConfigs-conformed) (clj->js stackNavigatorConfig))
      (createStackNavigator (clj->js routeConfigs-conformed)))))

(defn bottom-tab-navigator [routeConfigs navigationOptions]
  (let [routeConfigs-conformed (s/conform :react-navigation/RouteConfigs routeConfigs)]
    (assert (not= routeConfigs-conformed :cljs.spec.alpha/invalid))
    (createBottomTabNavigator (clj->js routeConfigs-conformed) (clj->js navigationOptions))))

(defn switch-navigator [routeConfigs switchNavigatorConfig]
  (let [routeConfigs-conformed          (s/conform :react-navigation/RouteConfigs routeConfigs)]
    (assert (not= routeConfigs-conformed :cljs.spec.alpha/invalid))
    (if switchNavigatorConfig
      (createSwitchNavigator (clj->js routeConfigs-conformed) (clj->js switchNavigatorConfig))
      (createSwitchNavigator (clj->js routeConfigs-conformed)))))

(defn twopane-navigator [routeConfigs stackNavigatorConfig]
  (createTwoPaneNavigator (clj->js routeConfigs) (clj->js stackNavigatorConfig)))
