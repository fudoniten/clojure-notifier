{
  description = "Library for sending DBus notifications.";

  inputs = {
    nixpkgs.url = "nixpkgs/nixos-22.11";
    utils.url = "github:numtide/flake-utils";
    helpers = {
      url = "git+https://git.fudo.org/fudo-public/nix-helpers.git";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, utils, helpers, ... }:
    utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages."${system}";
      in {
        packages = rec {
          default = notifier;
          notifier = helpers.packages."${system}".mkClojureLib {
            name = "org.fudo/notifier";
            src = ./.;
            buildCommand = "clojure -T:build uberjar";
          };
        };

        devShells = rec {
          default = updateDeps;
          updateDeps = pkgs.mkShell {
            buildInputs = with helpers.packages."${system}";
              [ updateClojureDeps ];
          };
        };
      });
}