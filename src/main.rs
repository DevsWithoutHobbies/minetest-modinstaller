#[macro_use] extern crate conrod;
extern crate piston_window;
extern crate find_folder;

use conrod::{
    Canvas,
    Colorable,
    Frameable,
    Positionable,
    Text,
    Widget,
    Button,
    DropDownList,
    Labelable,
    Sizeable,
    Slider,
    NumberDialer,
    TextBox,
    XYPad
};
use piston_window::{ UpdateEvent, PressEvent, OpenGL, PistonWindow, WindowSettings };

use conrod::{Theme, Scalar};
use conrod::color;

/// Conrod is backend agnostic. Here, we define the `piston_window` backend to use for our `Ui`.
type Backend = (piston_window::G2dTexture<'static>, piston_window::Glyphs);
type Ui = conrod::Ui<Backend>;
type UiCell<'a> = conrod::UiCell<'a, Backend>;





const TITLE: &'static str = "Minetest Modinstaller v0.1";

fn main() {
    let mut window: PistonWindow = WindowSettings::new(TITLE, (800, 600))
                                    .opengl(OpenGL::V3_2).exit_on_esc(false).vsync(true).build().unwrap();

    let mut ui = {
        let assets = find_folder::Search::KidsThenParents(3, 5)
            .for_folder("assets").unwrap();
        let font_path = assets.join("fonts/NotoSans/NotoSans-Regular.ttf");
        let theme = Theme::default();
        let glyph_cache = piston_window::Glyphs::new(&font_path, window.factory.clone()).unwrap();
        Ui::new(glyph_cache, theme)
    };


    while let Some(event) = window.next() {
        // Button/Mouse events
        if let Some(button) = event.press_args() {
            if button == piston_window::Button::Keyboard(piston_window::Key::LShift) {
                println!("Hallo");
            }
        }

        //Drawing
        ui.handle_event(&event);
        event.update(|_| ui.set_widgets(set_ui));
        window.draw_2d(&event, |c, g| ui.draw(c, g));
    }
}

fn set_ui(ref mut ui: UiCell) {
    widget_ids!{
        CANVAS,
        HEADER,
        BODY,
        LEFT_COLUMN,
        MODS_COLUMN,
        INFO_COLUMN,
        INSTALL_BUTTON_CANVAS,
        INSTALL_BUTTON,
        HEADER_TEXT,
    }

    const PAD: Scalar = 20.0;
    const INSTALL_BUTTON_HEIGHT: f64 = 40.0;

    let header = Canvas::new().color(color::WHITE).pad(PAD).length(100.0);
    let mods_column = Canvas::new().color(color::WHITE).scroll_kids().pad(PAD);
    let info_column = Canvas::new().color(color::WHITE).scroll_kids().pad(PAD);
    let install_button_canvas = Canvas::new().color(color::WHITE).pad(PAD).length(INSTALL_BUTTON_HEIGHT);



    Canvas::new()
    .frame(1.0)
    .color(color::LIGHT_RED)
    .flow_down(&[
         (HEADER, header),
         (BODY, Canvas::new().flow_right(&[
             (LEFT_COLUMN, Canvas::new().flow_down(&[
                (MODS_COLUMN, mods_column),
                (INSTALL_BUTTON_CANVAS,  install_button_canvas)
             ])),
             (INFO_COLUMN, info_column)
         ]))
      ])
    .set(CANVAS, ui);

    Text::new(TITLE)
        .color(color::DARK_GREEN)
        .font_size(40)
        .middle_of(HEADER)
        .align_text_middle()
        .line_spacing(2.5)
        .set(HEADER_TEXT, ui);

    Button::new()
        .w_h(1000.0, INSTALL_BUTTON_HEIGHT)
        .middle_of(INSTALL_BUTTON_CANVAS)
        .label(&"Install".to_string())
        .label_font_size(22)
        .color(color::LIGHT_GREEN)
        .react(|| {
            println!("Install");
        })
        .set(INSTALL_BUTTON, ui);
}
