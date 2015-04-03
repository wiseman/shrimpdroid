(defproject com.lemondronor/shrimpdroid "0.0.1-SNAPSHOT"
  :description "Android app in Clojure for controlling an AR.Drone."
  :url "https://github.com/wiseman/shrimpdroid"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :global-vars {*warn-on-reflection* true}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :plugins [[lein-droid "0.3.3"]]

  :dependencies [;; Need 0.1.7 of clj-tuple to fix an android issue.
                 [clj-tuple "0.1.7"]
                 [com.lemondronor/turboshrimp "0.3.7"]
                 [neko/neko "3.2.0-preview3"]
                 [org.clojure-android/clojure "1.7.0-alpha3" :use-resources true]]
  :profiles {:default [:dev]
             :dev
             [;; :android-common :android-user
              {:dependencies [[org.clojure-android/tools.nrepl "0.2.6"]]
               :target-path "target/debug"
               :android {:aot :all-with-unused
                         :rename-manifest-package "com.lemondronor.shrimpdroid.debug"
                         :manifest-options {:app-name "shrimpdroid - debug"}
                         ;; For some reason some of the turboshrimp
                         ;; debug messages seem to hang the app.
                         :ignore-log-priority [:debug :verbose]
                         }}]
             :release
             [;; :android-common
              {:target-path "target/release"
               :android
               { ;; Specify the path to your private keystore
                ;; and the the alias of the key you want to
                ;; sign APKs with.
                ;; :keystore-path "/home/user/.android/private.keystore"
                ;; :key-alias "mykeyalias"

                :ignore-log-priority [:debug :verbose]
                :aot :all
                :build-type :release}}]}

  :android {;; Specify the path to the Android SDK directory.
            :sdk-path "/Applications/adt-bundle-mac-x86_64-20131030/sdk"

            ;; Try increasing this value if dexer fails with
            ;; OutOfMemoryException. Set the value according to your
            ;; available RAM.
            :dex-opts ["-JXmx4096M"]

            ;; If previous option didn't work, uncomment this as well.
            ;; :force-dex-optimize true

            :target-version "19"
            :aot-exclude-ns [
                             "cider-nrepl.plugin"
                             "cider.nrepl"
                             "cljs-tooling.complete"
                             "cljs-tooling.info"
                             "cljs-tooling.util.analysis"
                             "cljs-tooling.util.misc"
                             "clojure.core.reducers"
                             "clojure.parallel"
                             ;; Required for turboshrimp.
                             "manifold.stream.async"
                             ]})
