#Introduction and project rationale

This project forms a large part of a PhD project which was aiming to automate the reading of Kato-Katz fecal smears for the identification and diagnosis of soil transmitted helminth (STH) infections. Helminths are parasitic worms which often infect people in areas of poor sanitation. The project uses a smartphone to control the microscope and analyse images for the presence of parasite eggs. 

#How is this different from other microscopy projects?

Much of the inspiration from this project comes from the OpenFlexure Microscope, which is a fantastic open hardware project developed by the Bath Open INstrumentation Group (BOING). I highly recommend viewing their webpage, as they have created several innovations to improve open hardware projects (including GitBuilding which these documentation pages were built with). 

[OpenFlexure webpage](https://openflexure.org/ ""){:target="_blank"}.

There are a few differences between this project and the OpenFlexure Microscope. The primary differences are that our microscope does not use flexure for the X and Y movement, and that our control software uses a separate app on the smartphone rather than using the Raspberry Pi directly. The decision to move away from flexures was to try to increase the speed and range of movement for the X and Y dimensions, which we hoped would allow us to scan large samples more quickly. The use of a smartphone was to try to remove the need for additional screens and peripherals (such as keyboards and mice) to keep the microscope device highly portable. 

#Should I build this microscope, or the OpenFlexure microscope? 

The OpenFlexure microscope is a much more mature project than ours, so I would definitely recommend checking out their documentation if you are considering which to build. If you need to scan a large area, or you are interested specifically in the problem of scanning Kato-Katz slides, then this microscope may be useful to you.

---

[Previous page](index_BOM.md) | [Next page](purchasing.md)